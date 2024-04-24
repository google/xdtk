// Copyright 2024 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using UnityEngine;

namespace Google.XR.XDTK {
    public class KeepOriginalScale : MonoBehaviour
    {
        Vector3 originalScale;
        Vector3 originalParentScale;

        // Start is called before the first frame update
        void Start()
        {
            originalScale = transform.localScale;
            originalParentScale = transform.parent.localScale;
        }

        // Update is called once per frame
        void Update()
        {
            var x = transform.parent.localScale.x / originalParentScale.x;
            var y = transform.parent.localScale.y / originalParentScale.y;
            var z = transform.parent.localScale.z / originalParentScale.z;
            transform.localScale = new Vector3 (originalScale.x/x,originalScale.y/y, originalScale.z/z);
        }
    }
}